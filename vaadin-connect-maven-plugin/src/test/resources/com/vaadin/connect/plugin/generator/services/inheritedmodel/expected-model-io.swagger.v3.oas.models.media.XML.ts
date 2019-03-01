export default interface XML {
  attribute: boolean;
  extensions: Map<string, any>;
  name: string;
  namespace: string;
  prefix: string;
  wrapped: boolean;
}
