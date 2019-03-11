// @ts-ignore
import client from './connect-client.default';
import NonNullModel from './com/vaadin/connect/plugin/generator/services/notnull/NotNullService/NonNullModel';

export function echoMap(
  shouldBeNotNull: boolean
): Promise<{ [key: string]: NonNullModel | null; } | null> {
  return client.call('NotNullService', 'echoMap', {shouldBeNotNull});
}

export function echoNonNullMode(
  nonNullModels: Array<NonNullModel | null>
): Promise<NonNullModel | null> {
  return client.call('NotNullService', 'echoNonNullMode', {nonNullModels});
}

export function getNonNullString(
  input: string
): Promise<string> {
  return client.call('NotNullService', 'getNonNullString', {input});
}
