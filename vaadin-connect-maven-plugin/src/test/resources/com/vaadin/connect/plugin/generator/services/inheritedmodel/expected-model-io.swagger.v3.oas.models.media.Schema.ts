import ExternalDocumentation from '../ExternalDocumentation';
import Discriminator from './Discriminator';
import XML from './XML';

export default interface Schema {
  ref: string;
  _default: any;
  _enum: any[];
  additionalProperties: any;
  deprecated: boolean;
  description: string;
  discriminator: Discriminator;
  example: any;
  exclusiveMaximum: boolean;
  exclusiveMinimum: boolean;
  extensions: Map<string, any>;
  externalDocs: ExternalDocumentation;
  format: string;
  maxItems: number;
  maxLength: number;
  maxProperties: number;
  maximum: number;
  minItems: number;
  minLength: number;
  minProperties: number;
  minimum: number;
  multipleOf: number;
  name: string;
  not: Schema;
  nullable: boolean;
  pattern: string;
  properties: Map<string, Schema>;
  readOnly: boolean;
  required: string[];
  title: string;
  type: string;
  uniqueItems: boolean;
  writeOnly: boolean;
  xml: XML;
}
