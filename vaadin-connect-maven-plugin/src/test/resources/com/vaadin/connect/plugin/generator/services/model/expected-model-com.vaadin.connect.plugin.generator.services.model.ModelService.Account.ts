import Group from './Group';
import ModelFromDifferentPackage from '../subpackage/ModelFromDifferentPackage';

export default interface Account {
  username: string;
  children: Account;
  groups: Group[];
  modelFromDifferentPackage: ModelFromDifferentPackage;
}
