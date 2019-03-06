import Role from './Role';

export default interface User {
  optionalField: string | null;
  name: string;
  password: string;
  roles: { [key: string]: Role; };
}
