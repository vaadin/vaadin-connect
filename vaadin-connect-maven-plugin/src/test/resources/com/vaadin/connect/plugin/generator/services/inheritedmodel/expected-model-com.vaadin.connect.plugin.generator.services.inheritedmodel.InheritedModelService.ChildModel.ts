import Version from '../../../../../../../fasterxml/jackson/core/Version';
import ParentModel from './ParentModel';
import ArraySchema from '../../../../../../../../io/swagger/v3/oas/models/media/ArraySchema';

export default interface ChildModel extends ParentModel {
  abc: Map<string, Version>[];
  def: Map<string, Map<string, Version>>[];
  name: string;
  testObject: ArraySchema;
}
