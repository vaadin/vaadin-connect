import Role from './Role';

export default interface User {
  name: string;
  optionalField: string | null;
  password: string;
  roles: { [key: string]: Role; };
}
