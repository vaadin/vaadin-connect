import Role from './Role';

export default interface User {
  name: string;
  password: string;
  roles: { [key: string]: Role; };
}
