(*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *)

open! IStd
module F = Format
module CallEvent = PulseCallEvent
module Invalidation = PulseInvalidation
module TaintItem = PulseTaintItem
module Timestamp = PulseTimestamp

module CellId = struct
  type t = int [@@deriving compare, equal, yojson_of]

  let pp = Int.pp

  let next_id = ref 0

  let () = AnalysisGlobalState.register_ref ~init:(fun () -> 0) next_id

  let next () =
    let id = !next_id in
    incr next_id ;
    id


  module Map = Int.Map
end

type event =
  | Allocation of {f: CallEvent.t; location: Location.t; timestamp: Timestamp.t}
  | Assignment of Location.t * Timestamp.t
  | Call of {f: CallEvent.t; location: Location.t; in_call: t; timestamp: Timestamp.t}
  | Capture of
      { captured_as: Pvar.t
      ; mode: CapturedVar.capture_mode
      ; location: Location.t
      ; timestamp: Timestamp.t }
  | ConditionPassed of
      {if_kind: Sil.if_kind; is_then_branch: bool; location: Location.t; timestamp: Timestamp.t}
  | CppTemporaryCreated of Location.t * Timestamp.t
  | FormalDeclared of Pvar.t * Location.t * Timestamp.t
  | Invalidated of PulseInvalidation.t * Location.t * Timestamp.t
  | NilMessaging of Location.t * Timestamp.t
  | Returned of Location.t * Timestamp.t
  | StructFieldAddressCreated of Fieldname.t RevList.t * Location.t * Timestamp.t
  | TaintSource of TaintItem.t * Location.t * Timestamp.t
  | TaintPropagated of Location.t * Timestamp.t
  | VariableAccessed of Pvar.t * Location.t * Timestamp.t
  | VariableDeclared of Pvar.t * Location.t * Timestamp.t

and t =
  | Epoch
  | Sequence of event * t
  | InContext of {main: t; context: t list}
  | BinaryOp of Binop.t * t * t
  | FromCellId of CellId.t * t
[@@deriving compare, equal]

let epoch = Epoch

let in_context_f from new_context_opt ~f =
  match new_context_opt with
  | None | Some [] ->
      f from
  | Some new_context -> (
    match from with
    | InContext {main; context} when phys_equal context new_context ->
        InContext {main= f main; context}
    | Epoch | Sequence _ | BinaryOp _ | InContext _ | FromCellId _ ->
        InContext {main= f from; context= new_context} )


let cell_id_first = function
  | (Epoch | InContext _ | BinaryOp _) as hist ->
      hist
  | Sequence (event, FromCellId (id, hist)) ->
      FromCellId (id, Sequence (event, hist))
  | FromCellId (id, FromCellId (_old_id, hist')) ->
      FromCellId (id, hist')
  | (Sequence _ | FromCellId _) as hist ->
      hist


let sequence ?context event hist =
  in_context_f hist context ~f:(fun hist -> Sequence (event, hist) |> cell_id_first)


let in_context context hist = in_context_f hist (Some context) ~f:Fn.id

let binary_op bop hist1 hist2 = BinaryOp (bop, hist1, hist2)

let from_cell_id id hist = FromCellId (id, hist) |> cell_id_first

let singleton event = Sequence (event, Epoch)

let get_cell_id = function
  | FromCellId (id, _) | InContext {main= FromCellId (id, _)} ->
      Some id
  | _ ->
      None


let location_of_event = function
  | Allocation {location}
  | Assignment (location, _)
  | Call {location}
  | Capture {location}
  | ConditionPassed {location}
  | CppTemporaryCreated (location, _)
  | FormalDeclared (_, location, _)
  | Invalidated (_, location, _)
  | NilMessaging (location, _)
  | Returned (location, _)
  | StructFieldAddressCreated (_, location, _)
  | TaintSource (_, location, _)
  | TaintPropagated (location, _)
  | VariableAccessed (_, location, _)
  | VariableDeclared (_, location, _) ->
      location


let timestamp_of_event = function
  | Allocation {timestamp}
  | Assignment (_, timestamp)
  | Call {timestamp}
  | Capture {timestamp}
  | ConditionPassed {timestamp}
  | CppTemporaryCreated (_, timestamp)
  | FormalDeclared (_, _, timestamp)
  | Invalidated (_, _, timestamp)
  | NilMessaging (_, timestamp)
  | Returned (_, timestamp)
  | StructFieldAddressCreated (_, _, timestamp)
  | TaintSource (_, _, timestamp)
  | TaintPropagated (_, timestamp)
  | VariableAccessed (_, _, timestamp)
  | VariableDeclared (_, _, timestamp) ->
      timestamp


let pop_least_timestamp ~main_only hists0 =
  let rec aux orig_hists_prefix curr_ts_hists_prefix latest_events (highest_t : Timestamp.t option)
      hists =
    match (highest_t, hists) with
    | _, [] ->
        (latest_events, curr_ts_hists_prefix)
    | _, Epoch :: hists ->
        aux orig_hists_prefix curr_ts_hists_prefix latest_events highest_t hists
    | _, FromCellId (_, hist) :: hists ->
        aux orig_hists_prefix curr_ts_hists_prefix latest_events highest_t (hist :: hists)
    | _, InContext {main} :: hists when main_only ->
        aux orig_hists_prefix curr_ts_hists_prefix latest_events highest_t (main :: hists)
    | _, InContext {main; context} :: hists ->
        aux orig_hists_prefix curr_ts_hists_prefix latest_events highest_t
          (main :: List.rev_append context hists)
    | _, BinaryOp (_, hist1, hist2) :: hists ->
        aux orig_hists_prefix curr_ts_hists_prefix latest_events highest_t (hist1 :: hist2 :: hists)
    | None, (Sequence (event, hist') as hist) :: hists ->
        aux (hist :: orig_hists_prefix) (hist' :: orig_hists_prefix) [event]
          (Some (timestamp_of_event event))
          hists
    | Some highest_ts, (Sequence (event, hist') as hist) :: hists
      when (timestamp_of_event event :> int) > (highest_ts :> int) ->
        aux (hist :: orig_hists_prefix) (hist' :: orig_hists_prefix) [event]
          (Some (timestamp_of_event event))
          hists
    | Some highest_ts, (Sequence (event, _) as hist) :: hists
      when (timestamp_of_event event :> int) < (highest_ts :> int) ->
        aux (hist :: orig_hists_prefix) (hist :: curr_ts_hists_prefix) latest_events highest_t hists
    | Some _, (Sequence (event, hist') as hist) :: hists
    (* when  timestamp_of_event _event = highest_ts *) ->
        aux (hist :: orig_hists_prefix) (hist' :: curr_ts_hists_prefix) (event :: latest_events)
          highest_t hists
  in
  aux [] [] [] None hists0


type iter_event =
  | EnterCall of CallEvent.t * Location.t
  | ReturnFromCall of CallEvent.t * Location.t
  | Event of event

let rec rev_iter_branches ~main_only hists ~f =
  if List.is_empty hists then ()
  else
    let latest_events, hists = pop_least_timestamp ~main_only hists in
    rev_iter_simultaneous_events ~main_only latest_events ~f ;
    rev_iter_branches ~main_only hists ~f


and rev_iter_simultaneous_events ~main_only events ~f =
  let is_nonempty = function
    | Epoch | FromCellId (_, Epoch) ->
        false
    | Sequence _ | InContext _ | BinaryOp _ | FromCellId _ ->
        true
  in
  let in_call = function Call {in_call} when is_nonempty in_call -> Some in_call | _ -> None in
  match events with
  | [] ->
      ()
  | event :: _ ->
      (* operate on just one representative, they should all be the same given they have the same
         timestamp inside the same path of the same procedure call *)
      ( match event with
      | Call {f= callee; location; in_call= in_call'} when is_nonempty in_call' ->
          f (ReturnFromCall (callee, location)) ;
          rev_iter_branches ~main_only (List.filter_map events ~f:in_call) ~f ;
          f (EnterCall (callee, location)) ;
          ()
      | _ ->
          () ) ;
      f (Event event)


and rev_iter ~main_only (history : t) ~f =
  match history with
  | Epoch ->
      ()
  | Sequence (event, rest) ->
      rev_iter_simultaneous_events ~main_only [event] ~f ;
      rev_iter ~main_only rest ~f
  | FromCellId (_, hist) ->
      rev_iter ~main_only hist ~f
  | InContext {main} when main_only ->
      rev_iter ~main_only main ~f
  | InContext {main; context} ->
      (* [not main_only] *)
      rev_iter_branches ~main_only (main :: context) ~f
  | BinaryOp (_, hist1, hist2) ->
      rev_iter_branches ~main_only [hist1; hist2] ~f


let rev_iter_main = rev_iter ~main_only:true

let iter ~main_only history ~f = Iter.rev (Iter.from_labelled_iter (rev_iter ~main_only history)) f

let yojson_of_event = [%yojson_of: _]

let yojson_of_t = [%yojson_of: _]

let pp_fields =
  let pp_sep fmt () = Format.pp_print_char fmt '.' in
  fun fmt fields -> Format.pp_print_list ~pp_sep Fieldname.pp fmt (RevList.to_list fields)


let pp_event_no_location fmt event =
  let pp_pvar fmt pvar =
    if Pvar.is_global pvar then F.fprintf fmt "global variable `%a`" Pvar.pp_value_non_verbose pvar
    else F.fprintf fmt "variable `%a`" Pvar.pp_value_non_verbose pvar
  in
  match event with
  | Allocation {f} ->
      F.fprintf fmt "allocated by call to %a" CallEvent.pp f
  | Assignment _ ->
      F.pp_print_string fmt "assigned"
  | Call {f} ->
      F.fprintf fmt "in call to %a" CallEvent.pp f
  | Capture {captured_as; mode} ->
      F.fprintf fmt "value captured %s as `%a`"
        (CapturedVar.string_of_capture_mode mode)
        Pvar.pp_value_non_verbose captured_as
  | ConditionPassed {if_kind; is_then_branch} ->
      ( match (is_then_branch, if_kind) with
      | true, Ik_if _ ->
          "taking \"then\" branch"
      | false, Ik_if _ ->
          "taking \"else\" branch"
      | true, (Ik_for | Ik_while | Ik_dowhile) ->
          "loop condition is true; entering loop body"
      | false, (Ik_for | Ik_while | Ik_dowhile) ->
          "loop condition is false; leaving loop"
      | true, Ik_switch ->
          "switch condition is true, entering switch case"
      | false, Ik_switch ->
          "switch condition is false, skipping switch case"
      | true, Ik_compexch ->
          "pointer contains expected value; writing desired to pointer"
      | false, Ik_compexch ->
          "pointer does not contain expected value; writing to expected"
      | true, (Ik_bexp _ | Ik_land_lor) ->
          "condition is true"
      | false, (Ik_bexp _ | Ik_land_lor) ->
          "condition is false" )
      |> F.pp_print_string fmt
  | CppTemporaryCreated _ ->
      F.pp_print_string fmt "C++ temporary created"
  | FormalDeclared (pvar, _, _) ->
      let pp_proc fmt pvar =
        Pvar.get_declaring_function pvar
        |> Option.iter ~f:(fun proc_name -> F.fprintf fmt " of %a" Procname.pp proc_name)
      in
      F.fprintf fmt "parameter `%a`%a" Pvar.pp_value_non_verbose pvar pp_proc pvar
  | Invalidated (invalidation, _, _) ->
      Invalidation.describe fmt invalidation
  | NilMessaging _ ->
      F.pp_print_string fmt "a message sent to nil returns nil"
  | Returned _ ->
      F.pp_print_string fmt "returned"
  | StructFieldAddressCreated (field_names, _, _) ->
      F.fprintf fmt "struct field address `%a` created" pp_fields field_names
  | TaintSource (taint_source, _, _) ->
      F.fprintf fmt "source of the taint here: %a" TaintItem.pp taint_source
  | TaintPropagated _ ->
      F.fprintf fmt "taint propagated"
  | VariableAccessed (pvar, _, _) ->
      F.fprintf fmt "%a accessed here" pp_pvar pvar
  | VariableDeclared (pvar, _, _) ->
      F.fprintf fmt "%a declared here" pp_pvar pvar


let pp_event fmt event =
  F.fprintf fmt "%a at %a :t%a" pp_event_no_location event Location.pp_line
    (location_of_event event) Timestamp.pp (timestamp_of_event event)


let pp fmt history =
  let rec pp_aux fmt = function
    | Epoch ->
        ()
    | FromCellId (id, hist) ->
        F.fprintf fmt "from_cell_id-%d;" id ;
        pp_aux fmt hist
    | Sequence ((Call {in_call} as event), tail) ->
        F.fprintf fmt "%a@;" pp_event event ;
        F.fprintf fmt "[@[%a@]]@;" pp_aux in_call ;
        pp_aux fmt tail
    | Sequence (event, tail) ->
        F.fprintf fmt "%a@;" pp_event event ;
        pp_aux fmt tail
    | InContext {main; context} ->
        F.fprintf fmt "(@[%a@]){@[%a@]}" (Pp.seq ~sep:"; " pp_aux) context pp_aux main
    | BinaryOp (bop, hist1, hist2) ->
        F.fprintf fmt "[@[%a@]] %a [@[%a@]]" pp_aux hist1 Binop.pp bop pp_aux hist2
  in
  F.fprintf fmt "@[%a@]" pp_aux history


let add_event_to_errlog ~nesting event errlog =
  let location = location_of_event event in
  let description = F.asprintf "%a" pp_event_no_location event in
  let tags = [] in
  Errlog.make_trace_element nesting location description tags :: errlog


let add_returned_from_call_to_errlog ~nesting f location errlog =
  let description = F.asprintf "return from call to %a" CallEvent.pp f in
  let tags = [] in
  Errlog.make_trace_element nesting location description tags :: errlog


let is_taint_event = function
  | Allocation _
  | Assignment _
  | Call _
  | Capture _
  | ConditionPassed _
  | CppTemporaryCreated _
  | FormalDeclared _
  | Invalidated _
  | NilMessaging _
  | Returned _
  | StructFieldAddressCreated _
  | VariableAccessed _
  | VariableDeclared _ ->
      false
  | TaintSource _ | TaintPropagated _ ->
      true


let add_to_errlog ?(include_taint_events = false) ~nesting history errlog =
  let nesting = ref nesting in
  let errlog = ref errlog in
  let one_iter_event = function
    | Event event ->
        if is_taint_event event && not include_taint_events then ()
        else errlog := add_event_to_errlog ~nesting:!nesting event !errlog
    | EnterCall _ ->
        decr nesting
    | ReturnFromCall (call, location) ->
        errlog := add_returned_from_call_to_errlog ~nesting:!nesting call location !errlog ;
        incr nesting
  in
  rev_iter ~main_only:false history ~f:one_iter_event ;
  !errlog


let get_first_main_event hist =
  Iter.head (Iter.from_labelled_iter (iter ~main_only:true hist))
  |> Option.bind ~f:(function Event event -> Some event | _ -> None)


let exists_main t ~f =
  Container.exists ~iter:rev_iter_main t ~f:(function Event event -> f event | _ -> false)
