/**
 * Status bean. Used only in request parameters to verify that request
 * parameter type descriptions are generated.
 */
export default interface Status {
  createdAt: Date;
  text: string;
}
