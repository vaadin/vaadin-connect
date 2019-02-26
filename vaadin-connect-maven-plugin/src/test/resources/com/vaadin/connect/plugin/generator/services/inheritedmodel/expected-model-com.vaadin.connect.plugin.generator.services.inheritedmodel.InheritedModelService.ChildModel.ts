import ParentModel from './ParentModel';
import ArraySchema from '../../../../../../../../io/swagger/v3/oas/models/media/ArraySchema';

export default interface ChildModel extends ParentModel {
  name: string;
  testObject: ArraySchema;
}
