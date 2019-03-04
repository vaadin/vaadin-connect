import Version from '../../../../../../../fasterxml/jackson/core/Version';
import ParentModel from './ParentModel';
import ArraySchema from '../../../../../../../../io/swagger/v3/oas/models/media/ArraySchema';

export default interface ChildModel extends ParentModel {
  abc: { [key: string]: Version; }[];
  def: { [key: string]: { [key: string]: Version; }; }[];
  name: string;
  testObject: ArraySchema;
}
