// @ts-ignore
import client from './connect-client.default';
import SameClassNameModel from './com/vaadin/connect/plugin/generator/services/sameclassname/SameClassNameService/SameClassNameModel';
import SameClassNameModel1 from './com/vaadin/connect/plugin/generator/services/sameclassname/subpackage/SameClassNameModel';

export function getMyClass(
  sameClassNameModel: SameClassNameModel1
): Promise<SameClassNameModel> {
  return client.call('SameClassNameService', 'getMyClass', {sameClassNameModel});
}
