open! IStd
(* module L = Logging *)

exception TODO

(** Abstract single object representation *)
module Objrep = struct
  type t = Location.t * Typ.t [@@deriving compare, sexp, equal, hash]

  let pp fmt (loc, typ) =
    let pp fmt t = Typ.pp Pp.text fmt t in
    Format.fprintf fmt "(%a:%a)" Location.pp_line loc pp typ
end

(** Abstract variable representation *)
module Varrep = struct
  type t = Expr of Exp.t | Field of Objrep.t * Fieldname.t [@@deriving compare, equal, hash]

  let pp fmt t =
    match t with
    | Expr e ->
        Format.fprintf fmt "%a@," Exp.pp e
    | Field (o, f) ->
        Format.fprintf fmt "%a.%a@," Objrep.pp o Fieldname.pp f
end

module ObjsrepDomain = struct
  module ObjrepSet = Set.Make (Objrep)

  include AbstractDomain.TopLifted (struct
    type t = ObjrepSet.t

    let pp fmt t = t |> ObjrepSet.to_list |> Format.pp_print_list Objrep.pp fmt

    let leq ~lhs ~rhs = if phys_equal lhs rhs then true else ObjrepSet.is_subset lhs ~of_:rhs

    let join = ObjrepSet.union

    let widen ~prev ~next ~num_iters:_ = join prev next
  end)

  let empty : t = NonTop ObjrepSet.empty

  let singleton obj_rep : t = NonTop (ObjrepSet.singleton obj_rep)

  let fold t ~init ~f =
    match (t : t) with Top -> init | NonTop objs -> ObjrepSet.fold objs ~init ~f


  let _add (t : t) (obj : Objrep.t) : t =
    match t with Top -> Top | NonTop objs -> NonTop (ObjrepSet.add objs obj)


  let union (t1 : t) (t2 : t) : t =
    match (t1, t2) with
    | NonTop objs1, NonTop objs2 ->
        NonTop (ObjrepSet.union objs1 objs2)
    | _ ->
        Top


  let _diff (t1 : t) (t2 : t) : t =
    match (t1, t2) with
    | NonTop objs1, NonTop objs2 ->
        NonTop (ObjrepSet.diff objs1 objs2)
    | _ ->
        Top


  let _inter (t1 : t) (t2 : t) : t =
    match (t1, t2) with
    | NonTop objs1, NonTop objs2 ->
        NonTop (ObjrepSet.inter objs1 objs2)
    | _ ->
        Top


  let _count (t : t) ~f : int option =
    match t with Top -> None | NonTop objs -> Some (ObjrepSet.count objs ~f)


  let _is_empty (t : t) = match t with Top -> false | NonTop objs -> ObjrepSet.is_empty objs
end

module Domain = AbstractDomain.Map (Varrep) (ObjsrepDomain)

let eval_expr (astate : Domain.t) (expr : Exp.t) (typ : Typ.t) loc : ObjsrepDomain.t =
  let open ObjsrepDomain in
  let eval_aux st v = match Domain.find_opt v st with Some objs -> objs | None -> empty in
  match expr with
  | Const (Cfun callee) when Procname.equal callee BuiltinDecl.__new ->
      (* TODO: mocked type_name *)
      singleton (loc, typ)
  | Var _ | Lvar _ ->
      eval_aux astate (Expr expr)
  | Lfield (exp, field_name, _) ->
      (* TODO: only consider a.b, not for a.b.c *)
      let objs = eval_aux astate (Expr exp) in
      fold objs ~init:empty ~f:(fun acc obj ->
          Field (obj, field_name) |> eval_aux astate |> union acc )
  | Lindex _ ->
      raise TODO
  | _ ->
      top


let eval_instr (astate : Domain.t) (instr : Sil.instr) : Domain.t =
  match instr with
  | Call ((id, id_typ), callee_expr, _args, _loc, _call_flags) ->
      let objs = eval_expr astate callee_expr id_typ _loc in
      Domain.add (Expr (Exp.Var id)) objs astate
  | Store {e1= Lvar v; typ; e2; loc} ->
      let objs = eval_expr astate e2 typ loc in
      Domain.add (Expr (Lvar v)) objs astate
  | Store {e1= Lfield (e, f, f_t); typ; e2; loc} ->
      let objs = eval_expr astate e2 typ loc in
      let vars = eval_expr astate e f_t loc in
      ObjsrepDomain.fold vars ~init:astate ~f:(fun acc var -> Domain.add (Field (var, f)) objs acc)
  | Store _ ->
      raise TODO
  | Load {id; typ; e; loc} ->
      let objs = eval_expr astate e typ loc in
      Domain.add (Expr (Exp.Var id)) objs astate
  | Prune _ ->
      astate
  | Metadata _ ->
      astate


module TransferFunctions = struct
  module CFG = ProcCfg.Normal
  module Domain = Domain

  type analysis_data = unit

  let exec_instr astate _ _node _ instr = eval_instr astate instr

  let pp_session_name node fmt =
    Format.fprintf fmt "devirtualizer analysis %a" CFG.Node.pp_id (CFG.Node.id node)
end

module Analyzer = AbstractInterpreter.MakeRPO (TransferFunctions)

(** [solve pdesc] is the potential pointing relationship of [pdesc], but flow-sensitive and
    context-insensitive *)
let solve pdesc =
  let _inv_map = Analyzer.exec_pdesc () ~initial:Domain.empty pdesc in
  let exit = Procdesc.get_exit_node pdesc in
  let st = Analyzer.extract_post (Procdesc.Node.get_id exit) _inv_map in
  match st with Some state -> Domain.pp Format.std_formatter state | None -> assert false
