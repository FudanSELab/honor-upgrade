(*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *)

open! IStd
module F = Format

(* domain definition *)
module CalleePair = struct
  type t = Procdesc.t * Location.t

  let compare (x0, y0) (x1, y1) =
    let x0 = Procdesc.get_loc x0 in
    let x1 = Procdesc.get_loc x1 in
    match Stdlib.compare x0 x1 with 0 -> Stdlib.compare y0 y1 | c -> c
end

module CalleeSet = Stdlib.Set.Make (CalleePair)

type t = {caller: Procdesc.t; callees: CalleeSet.t}

let leq ~lhs ~rhs =
  if phys_equal lhs rhs then true
  else match (lhs, rhs) with {callees= lees}, {callees= rees} -> CalleeSet.subset lees rees


let join a b =
  match (a, b) with
  | {caller; callees= a}, {callees= b} ->
      let callees = CalleeSet.union a b in
      {caller; callees}


let widen ~prev ~next ~num_iters:_ = join prev next

let pp fmt {caller; callees} =
  F.fprintf fmt "caller: {%a}\n" Procdesc.pp_signature caller ;
  CalleeSet.iter
    (fun (callee, _loc) -> F.fprintf fmt "callees: {%a}\n" Procdesc.pp_signature callee)
    callees


let init caller = {caller; callees= CalleeSet.empty}

let record_call {caller; callees} callee_procname loc =
  match Procdesc.load callee_procname with
  | Some callee_desc ->
      {caller; callees= CalleeSet.add (callee_desc, loc) callees}
  | None ->
      (* FIXME: callee not found *)
      {caller; callees}


let record_ref _astate = assert false

let has_leak _astate = false

type summary = t
(* end domain definition *)

let to_json t : ATDGenerated.Dependency_t.calling =
  let language = Language.to_json !Language.curr_language in
  let caller : Dependency_t.caller = {language; func= Procdesc.to_json t.caller} in
  let callees =
    let elems = CalleeSet.elements t.callees in
    List.rev_map elems ~f:(fun (pdesc, cs) : Dependency_t.callee ->
        {language; func= Procdesc.to_json pdesc; call_site= Location.to_json cs} )
  in
  {caller; callees}
