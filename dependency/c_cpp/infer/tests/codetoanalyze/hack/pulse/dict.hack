// Copyright (c) Facebook, Inc. and its affiliates.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

namespace DictTests;

class Main {

  public function init_and_load_bad(int $u, int $v, int $w) {
    $tainted = \Level1\taintSource();

    $t1 = dict['a' => $u, 'b' => $v];

    if ($t1['a'] == $u && $t1['b'] == $v) {
      \Level1\taintSink($tainted);
    }
  }

  public function init_and_load_good(int $u, int $v, int $w) {
    $tainted = \Level1\taintSource();

    $t1 = dict['a' => $u, 'b' => $v];

    if ($t1['a'] != $u || $t1['b'] != $v) {
      \Level1\taintSink($tainted);
    }
  }

  public function copy_on_write_bad(int $u, int $v, int $w) {
    $tainted = \Level1\taintSource();

    $t1 = dict['a' => $u, 'b' => $v];

    $t2 = $t1;

    $t2['a'] = $w;

    if ($t1['a'] == $u && $t2['a'] == $w && $t1['b'] == $v && $t2['b'] == $v) {
      \Level1\taintSink($tainted);
    }
  }

  public function copy_on_write_good(int $u, int $v, int $w) {
    $tainted = \Level1\taintSource();

    $t1 = dict['a' => $u, 'b' => $v];

    $t2 = $t1;

    $t2['a'] = $w;

    if ($t1['a'] != $u || $t2['a'] != $w || $t1['b'] != $v || $t2['b'] != $v) {
      \Level1\taintSink($tainted);
    }
  }

  public function multidim_copy_on_write_bad(
    int $u1,
    int $v1,
    int $u2,
    int $v2,
    int $w,
  ) {
    $tainted = \Level1\taintSource();

    $t1 = dict[
      'level1' => dict['a' => $u1, 'b' => $v1],
      'level2' => dict['a' => $u2, 'b' => $v2],
    ];

    $t2 = $t1['level2'];

    $t1['level2']['a'] = $w;

    if (
      $t1['level1']['a'] == $u1 &&
      $t1['level1']['b'] == $v1 &&
      $t1['level2']['a'] == $w &&
      $t2['a'] == $u2 &&
      $t1['level2']['b'] == $v2 &&
      $t2['b'] == $v2
    ) {
      \Level1\taintSink($tainted);
    }
  }

  public function multidim_copy_on_write_good(
    int $u1,
    int $v1,
    int $u2,
    int $v2,
    int $w,
  ) {
    $tainted = \Level1\taintSource();

    $t1 = dict[
      'level1' => dict['a' => $u1, 'b' => $v1],
      'level2' => dict['a' => $u2, 'b' => $v2],
    ];

    $t2 = $t1['level2'];

    $t1['level2']['a'] = $w;

    if ($t1['level1']['a'] != $u1) {
      \Level1\taintSink($tainted);
    }
    if ($t1['level1']['b'] != $v1) {
      \Level1\taintSink($tainted);
    }
    if ($t1['level2']['a'] != $w) {
      \Level1\taintSink($tainted);
    }
    if ($t2['a'] != $u2) {
      \Level1\taintSink($tainted);
    }
    if ($t1['level2']['b'] != $v2) {
      \Level1\taintSink($tainted);
    }
    if ($t2['b'] != $v2) {
      \Level1\taintSink($tainted);
    }
  }

  public function copy_on_write_no_dynamic_type_bad(dict<string, int> $dict) {
    $tainted = \Level1\taintSource();
    $dict['a'] = 1;
    \Level1\taintSink($tainted);
  }
}
