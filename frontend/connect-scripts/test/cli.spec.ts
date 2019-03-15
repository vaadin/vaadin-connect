/// <reference types="intern"/>
const {describe, it, beforeEach, afterEach} = intern.getPlugin('interface.bdd');
const {expect} = intern.getPlugin('chai');

import {SinonStatic} from 'sinon';
const {sinon} = intern.getPlugin<{sinon: SinonStatic}>('sinon');

import {ChildProcess, SpawnSyncReturns} from 'child_process';
import {Console} from 'console';
import {EventEmitter} from 'events';
import {Readable, Writable} from 'stream';

import path = require('path');

import crossSpawn = require('cross-spawn');
import proxyquire = require('proxyquire');

class MockInput extends Readable {
  data: string = '';

  constructor() {
    super();
  }

  _read(size: number) {
    let accepted: boolean;
    do {
      accepted = this.push(this.data.slice(0, size));
      this.data = this.data.slice(size);
    } while (accepted);
  }
}
type InputStub = MockInput & NodeJS.ReadStream;

class MockOutput extends Writable {
  data: string = '';

  constructor() {
    super({decodeStrings: false});
  }

  _write(chunk: string, _: string, callback: () => void) {
    this.data += chunk;
    callback();
  }
}
type OutputStub = MockOutput & NodeJS.WriteStream;

const spawnedProcessesMap = new Map<number, MockChildProcess>();

class MockChildProcess extends EventEmitter {
  static lastPid: number = 1000;
  pid: number;
  killed: boolean;

  constructor(
    public cmd: string,
    public args: string[]
  ) {
    super();
    this.pid = MockChildProcess.lastPid++;
    spawnedProcessesMap.set(this.pid, this);
    this.killed = false;
  }

  toString() {
    return `$ ${this.cmd} ${this.args.join(' ')}`;
  }

  exit(exitCode: number = 0) {
    this.emit('exit', exitCode);
    spawnedProcessesMap.delete(this.pid);
    delete this.pid;
  }
}

interface StubNs {
  ['@global']?: boolean;
  ['@runtimeGlobal']?: boolean;
}

type CrossSpawnStubFn = sinon.SinonStubbedMember<typeof crossSpawn>;
interface CrossSpawnStubNs extends StubNs {
  sync: sinon.SinonStubbedMember<typeof crossSpawn.sync>;
}
type CrossSpawnStub = CrossSpawnStubFn & CrossSpawnStubNs;

const fakeSpawn = (cmd: string, args: string[] = []) => {
  const mockChildProcess = (
    new MockChildProcess(cmd, args) as any
  ) as MockChildProcess & ChildProcess;
  console.log(`Starting ${mockChildProcess}`);
  return mockChildProcess;
};
const crossSpawnStub = sinon.stub() as CrossSpawnStub;
crossSpawnStub['@global'] = true;
crossSpawnStub['@runtimeGlobal'] = true;

const fakeSpawnSync = (cmd: string, args: ReadonlyArray<string> = []) => {
  console.log(`Sync run $ ${cmd} ${args.join(' ')}`);
  return {status: 0} as SpawnSyncReturns<Buffer>;
};
crossSpawnStub.sync = sinon.stub();

type KillStubFn = sinon.SinonStubbedMember<typeof process.kill>;
interface KillStubNs extends StubNs { }
type KillStub = KillStubFn & KillStubNs;

const fakeKill = (pid: number) => {
  const mockChildProcess = spawnedProcessesMap.get(pid);
  if (mockChildProcess) {
    console.log(`Killing ${mockChildProcess}`);
    mockChildProcess.killed = true;
    mockChildProcess.exit(137);
  }
};
const killStub = sinon.stub() as KillStub;
killStub['@global'] = true;
killStub['@runtimeGlobal'] = true;

interface EnsureExecutableStub extends StubNs {
  ensureExecutable: sinon.SinonStubbedMember<(filepath: string) => void>
}
const ensureExecutableStub = {
  ensureExecutable: sinon.stub()
} as EnsureExecutableStub;
ensureExecutableStub['@global'] = true;
ensureExecutableStub['@runtimeGlobal'] = true;

// When stubbing process stdout and stderr during running tests, the regular
// console is silenced. Here is a console instance that still works.
const console = new Console(process.stdout, process.stderr);

class MockProcess extends EventEmitter {
  stdin: InputStub;
  stdout: OutputStub;
  stderr: OutputStub;
  exitCode?: number;
  argv: string[];
  env: NodeJS.ProcessEnv;

  constructor() {
    super();
    this.argv = process.argv.slice(0);
    this.env = Object.assign({}, process.env);
    this.stdin = new MockInput() as InputStub;
    this.stdout = new MockOutput() as OutputStub;
    this.stderr = new MockOutput() as OutputStub;
  }

  cwd() {
    return path.resolve('../../vaadin-connect-demo');
  }

  exit(exitCode?: number) {
    this.exitCode = exitCode;
    this.emit('exit', exitCode);
  }
}

describe('CLI integration', () => {
  const sandbox = sinon.createSandbox();
  let mockProcess: MockProcess;
  let mockConsole: Console;

  beforeEach(() => {
    mockProcess = new MockProcess();
    mockConsole = new Console(mockProcess.stdout, mockProcess.stderr);
    sandbox.replace(
      global,
      'process',
      (mockProcess as unknown) as NodeJS.Process
    );
    sandbox.replace(global, 'console', mockConsole);

    crossSpawnStub.callsFake(fakeSpawn);
    crossSpawnStub.sync.callsFake(fakeSpawnSync);
    killStub.callsFake(fakeKill);
  });

  afterEach(() => {
    // Expect no running processes left behind
    expect(spawnedProcessesMap.size).to.equal(0);
    // console.log(mockProcess.stdout.data); // tslint:disable-line
    // console.log(mockProcess.stderr.data); // tslint:disable-line

    sandbox.restore();

    crossSpawnStub.resetHistory();
    crossSpawnStub.sync.resetHistory();
    killStub.resetHistory();
    ensureExecutableStub.ensureExecutable.resetHistory();
  });

  function runCli(args: string[]): Promise<void> {
    mockProcess.argv = ['node', 'connect-scripts.js', ...args];
    console.group(`Running $ connect-scripts ${args.join(' ')} `);
    return new Promise(async(resolve) => {
      process.on('exit', (exitCode: number = 0) => {
        console.log(`Exited with code ${exitCode}.`);
        resolve();
        return undefined as never;
      });
      try {
        await proxyquire.noCallThru()(
          '../cli',
          {
            'cross-spawn': crossSpawnStub,
            'tree-kill': killStub,
            './lib/ensureExecutable': ensureExecutableStub
          }
        ).run();
        process.exit(0);
      } catch (error) {
        console.log(`Exit with an error:`);
        console.log(error);
        process.exitCode = 1;
        resolve();
      } finally {
        console.groupEnd();
      }
    });
  }

  it('should fail without arguments', async() => {
    await runCli([]);

    expect(process.exitCode).to.equal(1);
  });

  it('should run build', async() => {
    await runCli(['build']);

    expect(crossSpawnStub.sync).to.be.calledOnce;
    expect(crossSpawnStub.sync).to.be.calledWith(
      './mvnw',
      sinon.match.array.deepEquals(['-e', 'package']),
      sinon.match.has('stdio', 'inherit')
    );
    expect(ensureExecutableStub.ensureExecutable)
      .to.be.calledBefore(crossSpawnStub.sync);

    expect(process.exitCode).to.equal(0);
  });

  it('should run build:frontend', async() => {
    await runCli(['build:frontend']);

    expect(crossSpawnStub.sync).to.be.calledOnce;
    expect(crossSpawnStub.sync).to.be.calledWith(
      'node',
      sinon.match.array.startsWith([
        './node_modules/.bin/webpack'
      ]),
      sinon.match.has('stdio', 'inherit')
    );

    expect(process.exitCode).to.equal(0);
  });

  it('should run start', async() => {
    crossSpawnStub.callsFake((cmd: string, args: string[] = []) => {
      console.log('in', crossSpawnStub.callCount);

      const mockChildProcess = fakeSpawn(cmd, args);
      setTimeout(() => mockChildProcess.exit(0));
      return mockChildProcess;
    });

    await runCli(['start']);

    expect(process.exitCode).to.equal(0);
  });

  it('should run test', async() => {
    crossSpawnStub.callsFake((cmd: string, args: string[] = []) => {
      expect(crossSpawnStub.sync.callCount).to.equal(2);
      expect(crossSpawnStub.sync.getCall(0)).to.be.calledWith(
        'node',
        sinon.match.array.deepEquals([
          './node_modules/.bin/intern',
          'environments=node',
          'functionalSuites='
        ]),
        sinon.match.has('stdio', 'inherit')
      );

      expect(crossSpawnStub.sync.getCall(1)).to.be.calledWith(
        'node',
        sinon.match.array.startsWith(['./node_modules/.bin/webpack'])
          .and(sinon.match.array.contains(['--mode', 'development'])),
        sinon.match.has('stdio', 'inherit')
      );

      expect(crossSpawnStub).to.be.calledOnce;
      expect(crossSpawnStub).to.be.calledWith(
        './mvnw',
        sinon.match.array.deepEquals([
          '-e',
          'compile',
          'spring-boot:start',
          '-Dspring-boot.run.fork'
        ])
      );

      expect(ensureExecutableStub.ensureExecutable)
        .to.be.calledBefore(crossSpawnStub);

      const mockChildProcess = fakeSpawn(cmd, args);
      setTimeout(() => mockChildProcess.exit(0));
      return mockChildProcess;
    });

    await runCli(['test']);

    expect(crossSpawnStub.sync.callCount).to.equal(4);
    expect(crossSpawnStub.sync.getCall(2)).to.be.calledWith(
      'node', 
      sinon.match.array.deepEquals([
        './node_modules/.bin/intern'
      ])
    );
    expect(crossSpawnStub.sync.getCall(3)).to.be.calledWith(
      './mvnw', 
      sinon.match.array.deepEquals([
        '-e',
        'spring-boot:stop',
        '-Dspring-boot.stop.fork',
        '-q'
      ])
    );

    expect(process.exitCode).to.equal(0);
  });

  it('should run test:e2e', async() => {
    await runCli(['test:e2e']);

    expect(crossSpawnStub.sync).to.be.calledOnce;
    expect(crossSpawnStub.sync.getCall(0)).to.be.calledWith(
      'node',
      sinon.match.array.deepEquals([
        './node_modules/.bin/intern'
      ]),
      sinon.match.has('stdio', 'inherit')
    );

    expect(process.exitCode).to.equal(0);
  });

  it('should run test:unit', async() => {
    await runCli(['test:unit']);

    expect(crossSpawnStub.sync).to.be.calledOnce;
    expect(crossSpawnStub.sync.getCall(0)).to.be.calledWith(
      'node',
      sinon.match.array.deepEquals([
        './node_modules/.bin/intern',
        'environments=node',
        'functionalSuites='
      ]),
      sinon.match.has('stdio', 'inherit')
    );

    expect(process.exitCode).to.equal(0);
  });
});
