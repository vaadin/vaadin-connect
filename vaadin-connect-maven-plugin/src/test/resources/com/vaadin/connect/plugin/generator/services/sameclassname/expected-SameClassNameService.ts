// @ts-ignore
import client from './connect-client.default';
import SameClassNameModel from './com/vaadin/connect/plugin/generator/services/sameclassname/SameClassNameService/SameClassNameModel';
import SubpackageSameClassNameModel from './com/vaadin/connect/plugin/generator/services/sameclassname/subpackage/SameClassNameModel';

export function getMyClass(
  sameClassNameModel: SubpackageSameClassNameModel[]
): Promise<SameClassNameModel> {
  return client.call('SameClassNameService', 'getMyClass', {sameClassNameModel});
}

export function getSubpackageModelList(
  sameClassNameModel: Map<string, SubpackageSameClassNameModel>
): Promise<SubpackageSameClassNameModel[]> {
  return client.call('SameClassNameService', 'getSubpackageModelList', {sameClassNameModel});
}

export function getSubpackageModelMap(
  sameClassNameModel: Map<string, SameClassNameModel>
): Promise<Map<string, SubpackageSameClassNameModel>> {
  return client.call('SameClassNameService', 'getSubpackageModelMap', {sameClassNameModel});
}

export function getSubpackageModel(): Promise<SubpackageSameClassNameModel> {
  return client.call('SameClassNameService', 'getSubpackageModel');
}

export function setSubpackageModel(
  model: SubpackageSameClassNameModel
): Promise<void> {
  return client.call('SameClassNameService', 'setSubpackageModel', {model});
}
