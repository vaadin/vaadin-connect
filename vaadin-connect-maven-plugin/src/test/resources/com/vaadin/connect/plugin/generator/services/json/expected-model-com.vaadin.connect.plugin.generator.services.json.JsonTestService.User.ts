import Role from './Role';

export default interface User {
  a: string | null;
  name: string;
  password: string;
  roles: { [key: string]: Role; };
}
