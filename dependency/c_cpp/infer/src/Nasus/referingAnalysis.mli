type t

val solve : Procdesc.t list -> t

val to_json : t -> ATDGenerated.Dependency_t.reference list