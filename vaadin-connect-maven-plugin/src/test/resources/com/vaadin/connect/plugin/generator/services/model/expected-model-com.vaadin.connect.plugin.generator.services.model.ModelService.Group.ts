import Account from './Account';

export default interface Group {
  groupId: string;
  groupName: string;
  creator: Account;
}
