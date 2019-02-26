import SubProperty from './SameClassNameModel/SubProperty';
import SubProperty as SubProperty1 from './SubProperty';

export default interface SameClassNameModel {
  bar: string;
  barbarfoo: SubProperty1;
  foofoo: SubProperty;
}
