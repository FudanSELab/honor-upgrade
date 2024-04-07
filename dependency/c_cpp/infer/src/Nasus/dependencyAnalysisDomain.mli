(*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *)

open! IStd

include AbstractDomain.S

val init : Procdesc.t -> t

val record_call : t -> Procname.t -> Location.t -> t

val record_ref : t -> t

val has_leak : t -> bool

type summary = t
(* domain definition *)

val to_json : t -> ATDGenerated.Dependency_t.calling
