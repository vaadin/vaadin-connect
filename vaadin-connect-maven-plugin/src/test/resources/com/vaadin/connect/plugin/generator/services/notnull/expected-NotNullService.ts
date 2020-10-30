// @ts-ignore
import client from './connect-client.default';
import NonNullModel from './com/vaadin/connect/plugin/generator/services/notnull/NotNullService/NonNullModel';
import ParameterType from './com/vaadin/connect/plugin/generator/services/notnull/NotNullService/ParameterType';
import ReturnType from './com/vaadin/connect/plugin/generator/services/notnull/NotNullService/ReturnType';

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

export function getNotNullGenericParameterType(): Promise<Array<ReturnType>> {
  return client.call('NotNullService', 'getNotNullGenericParameterType');
}

export function getNotNullReturnType(): Promise<ReturnType> {
  return client.call('NotNullService', 'getNotNullReturnType');
}

export function sendNotNullGenericParameterType(
  collection: Array<ParameterType>
): Promise<void> {
  return client.call('NotNullService', 'sendNotNullGenericParameterType', {collection});
}

export function sendParameterType(
  parameterType: ParameterType
): Promise<void> {
  return client.call('NotNullService', 'sendParameterType', {parameterType});
}
