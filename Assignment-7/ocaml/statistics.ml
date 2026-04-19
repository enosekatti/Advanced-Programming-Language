let read_ints_from_file filename =
  let channel = open_in filename in
  let rec loop acc =
    try
      let value = Scanf.fscanf channel " %d" (fun x -> x) in
      loop (value :: acc)
    with End_of_file ->
      close_in channel;
      List.rev acc
  in
  loop []

let mean numbers =
  let total = List.fold_left ( + ) 0 numbers in
  float_of_int total /. float_of_int (List.length numbers)

let median numbers =
  let sorted = List.sort compare numbers in
  let n = List.length sorted in
  if n mod 2 = 1 then
    float_of_int (List.nth sorted (n / 2))
  else
    let left = List.nth sorted ((n / 2) - 1) in
    let right = List.nth sorted (n / 2) in
    (float_of_int left +. float_of_int right) /. 2.0

let mode numbers =
  let sorted = List.sort compare numbers in
  let rec count_runs lst current_value current_count acc =
    match lst with
    | [] -> List.rev ((current_value, current_count) :: acc)
    | x :: xs ->
        if x = current_value then
          count_runs xs current_value (current_count + 1) acc
        else
          count_runs xs x 1 ((current_value, current_count) :: acc)
  in
  match sorted with
  | [] -> ([], 0)
  | x :: xs ->
      let frequencies = count_runs xs x 1 [] in
      let max_count = List.fold_left (fun m (_, c) -> max m c) 0 frequencies in
      if max_count = 1 then
        ([], 1)
      else
        let modes =
          frequencies
          |> List.filter (fun (_, c) -> c = max_count)
          |> List.map fst
        in
        (modes, max_count)

let join_ints values =
  values
  |> List.map string_of_int
  |> String.concat ", "

let () =
  let values = read_ints_from_file "../tests/sample_input.txt" in
  let count = List.length values in
  if count = 0 then (
    print_endline "No integers found in input.";
    exit 1
  );

  Printf.printf "OCaml Functional Statistics\n";
  Printf.printf "Input count: %d\n" count;
  Printf.printf "Mean: %.2f\n" (mean values);
  Printf.printf "Median: %.2f\n" (median values);

  let modes, frequency = mode values in
  match modes with
  | [] ->
      if frequency = 1 then
        print_endline "Mode: no mode (all values unique)"
      else
        print_endline "Mode: no data"
  | _ ->
      Printf.printf "Mode: %s (frequency %d)\n" (join_ints modes) frequency
