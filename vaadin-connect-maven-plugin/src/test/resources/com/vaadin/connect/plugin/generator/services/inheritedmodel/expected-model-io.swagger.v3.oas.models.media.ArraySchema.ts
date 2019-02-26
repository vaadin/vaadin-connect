import ParentModel from './ParentModel';
import ArraySchema from '../../../../../../../../io/swagger/v3/oas/models/media/ArraySchema';

export default interface ChildModel extends ParentModel {
  abc: Map<string, Version>[];
  name: string;
  testObject: ArraySchema;
}
