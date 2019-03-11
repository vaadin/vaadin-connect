// @ts-ignore
import client from './connect-client.default';
import Account from './com/vaadin/connect/plugin/generator/services/model/ModelService/Account';
import Group from './com/vaadin/connect/plugin/generator/services/model/ModelService/Group';

export function getComplexTypeParams(
  accounts: Array<Account | null> | null,
  groups: { [key: string]: Group | null; } | null
): Promise<void> {
  return client.call('ComplexTypeParamsService', 'getComplexTypeParams', {accounts, groups});
}
