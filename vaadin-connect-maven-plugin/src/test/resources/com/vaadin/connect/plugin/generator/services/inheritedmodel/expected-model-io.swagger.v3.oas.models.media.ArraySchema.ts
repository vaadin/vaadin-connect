import ExternalDocumentation from '../ExternalDocumentation';
import Discriminator from './Discriminator';
import Schema from './Schema';
import XML from './XML';

export default interface ArraySchema extends Schema {
  items: Schema;
  type: string;
}
