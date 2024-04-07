type t

val solve : Tenv.t -> t

val to_json : t -> Dependency_t.inheritance