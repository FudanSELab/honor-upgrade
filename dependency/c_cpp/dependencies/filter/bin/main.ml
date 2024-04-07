open Core

let do_preserve cc dir =
  let open Yojson.Basic.Util in
  let json = Yojson.Basic.from_file cc in
  let cmds = json |> to_list in
  let pred cmd =
    let file = cmd |> member "file" |> to_string in
    String.is_prefix file ~prefix:dir
  in
  let pred_json = List.filter cmds ~f:pred in
  let buf = Buffer.create 16 in
  Yojson.Basic.write_list buf pred_json ;
  Out_channel.write_all "./preserved.json" ~data:(Buffer.contents buf)
  (* Yojson.Basic.seq_to_file "./preserved.json" dd *)

let command =
  Command.basic
    ~summary:"Preserve compile commands of the input dir"
    ~readme:(fun () -> "More detailed information")
    Command.Param.(
      map
        (both
           (anon ("cc" %: string))
           (anon ("dir" %: string)))
        ~f:(fun (cc, dir) () ->
          do_preserve cc dir))

let () = Command_unix.run ~version:"1.0" ~build_info:"RWO" command