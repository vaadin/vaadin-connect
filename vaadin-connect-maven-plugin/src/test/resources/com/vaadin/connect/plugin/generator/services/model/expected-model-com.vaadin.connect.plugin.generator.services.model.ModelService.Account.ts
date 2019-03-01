import Group from './Group';
import ModelFromDifferentPackage from '../subpackage/ModelFromDifferentPackage';

export default interface Account {
  children: Account;
  /**
   * Multiple line description should work.This is very very very very very
   * very very very long.
   */
  groups: Group[];
  modelFromDifferentPackage: ModelFromDifferentPackage;
  /**
   * Javadoc for username.
   */
  username: string;
}
