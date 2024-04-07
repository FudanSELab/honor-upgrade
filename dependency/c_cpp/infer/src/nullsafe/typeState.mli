(*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *)

open! IStd

(** Module for typestates: maps from expressions to annotated types, with extensions. *)

(** Typestate *)
type t

type range = Typ.t * InferredNullability.t

val pp_range : Format.formatter -> Typ.t * InferredNullability.t -> unit

val add_id : Ident.t -> range -> t -> descr:string -> t
(** [descr] is for debug logs only *)

val add : Pvar.t -> range -> t -> descr:string -> t
(** [descr] is for debug logs only *)

val empty : t

val equal : t -> t -> bool

val join : t -> t -> t

val lookup_id : Ident.t -> t -> range option

val lookup_pvar : Pvar.t -> t -> range option

val pp : Format.formatter -> t -> unit

val remove_id : Ident.t -> t -> descr:string -> t
(** [descr] is for debug logs only *)
