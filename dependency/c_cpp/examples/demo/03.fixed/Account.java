/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

@ThreadSafe
public interface Account {
  void deposit(int amount);

  int withdraw(int amount);
}
