(*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *)

open! IStd

type t [@@deriving compare]

val make : CallSite.t -> t

val site : t -> CallSite.t

module Set : PrettyPrintable.PPSet with type elt = t
