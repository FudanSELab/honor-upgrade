val pp : Format.formatter -> unit

val subtypes_of : Typ.name -> Typ.name list

val ( <: ) : Typ.name -> Typ.name -> bool
