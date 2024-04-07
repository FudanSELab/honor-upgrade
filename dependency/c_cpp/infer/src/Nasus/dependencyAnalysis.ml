(*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *)

open! IStd
module F = Format
(* module L = Logging *)

(* checker definition *)
module TransferFunctions (CFG : ProcCfg.S) = struct
  module CFG = CFG
  module Domain = DependencyAnalysisDomain

  type analysis_data = DependencyAnalysisDomain.t InterproceduralAnalysis.t

  (** Take an abstract state and instruction, produce a new abstract state *)
  let exec_instr (astate : DependencyAnalysisDomain.t)
      {InterproceduralAnalysis.proc_desc= _; tenv; analyze_dependency= _; _} _ _ (instr : Sil.instr)
      =
    match instr with
    | Call (_, Const (Const.Cfun callee_procname), (_, typ) :: _, loc, call_flag) ->
        (* function call [return_opt] := invoke [callee_procname]([actuals]) *)
        if call_flag.cf_virtual then
          match typ.desc with
          | Tptr ({desc= Tstruct _typ_name}, _) ->
              (* TODO: be about to use CHA | PTA *)
              let methods = MethodDispatch.resolve_method CHA tenv callee_procname _typ_name in
              List.fold methods ~init:astate ~f:(fun acc methd ->
                  DependencyAnalysisDomain.record_call acc methd loc )
              (* astate *)
          | Tstruct _typ_name ->
              astate
          | _ ->
              astate
        else
          (* Nasus.MethodDispatch.resolve_method CHA tenv callee_procname  *)
          DependencyAnalysisDomain.record_call astate callee_procname loc
    | _ ->
        astate


  let pp_session_name _node fmt = F.pp_print_string fmt "resource leaks"
end

(** 5(a) Type of CFG to analyze--Exceptional to follow exceptional control-flow edges, Normal to
    ignore them *)
module CFG = ProcCfg.Normal

(* Create an intraprocedural abstract interpreter from the transfer functions we defined *)
module Analyzer = AbstractInterpreter.MakeRPO (TransferFunctions (CFG))

(** Report an error when we have acquired more resources than we have released *)
let report_if_leak {InterproceduralAnalysis.proc_desc; err_log; _} post =
  if DependencyAnalysisDomain.has_leak post then
    let last_loc = Procdesc.Node.get_loc (Procdesc.get_exit_node proc_desc) in
    let message = F.asprintf "Leaked %a resource(s)" DependencyAnalysisDomain.pp post in
    Reporting.log_issue proc_desc err_log ~loc:last_loc DependencyAnalysis
      IssueType.lab_resource_leak message


(** Main function into the checker--registered in RegisterCheckers *)
let checker ({InterproceduralAnalysis.proc_desc} as analysis_data) =
  let result =
    Analyzer.compute_post analysis_data ~initial:(DependencyAnalysisDomain.init proc_desc) proc_desc
  in
  Option.iter result ~f:(fun post -> report_if_leak analysis_data post) ;
  result
(* end checker definition *)
