// @ts-ignore
import client from './connect-client.default';
import Account from './com/vaadin/connect/plugin/generator/services/model/ModelService/Account';
import Group from './com/vaadin/connect/plugin/generator/services/model/ModelService/Group';

export function getComplexTypeParams(
  accounts: Account[],
  groups: { [key: string]: Group; }
): Promise<void> {
  return client.call('ComplexTypeParamsService', 'getComplexTypeParams', {accounts, groups});
}
