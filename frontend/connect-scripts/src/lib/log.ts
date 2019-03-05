/**
 * Log categories
 */
export enum LogCategory {
  Error = 'error',
  Warn = 'warn',
  Success = 'success',
  Progress = 'progress',
  Debug = 'debug'
}

/**
 * Decorates a message with Vaadin Connect prefix.
 */
function decorateLogMessage(category: LogCategory, ...args: any[]): string {
  const messageParts: string[] = args.map(String);

  const iconsByLogCategory: {[key in LogCategory]?: string} = {
    [LogCategory.Error]: '🚫',
    [LogCategory.Warn]: '⚠️',
    [LogCategory.Success]: '🚀',
    [LogCategory.Progress]: '🌀',
  };
  const icon = iconsByLogCategory[category];
  if (icon) {
    // Prepend the project name in color
    messageParts.unshift('\x1b[36mVaadin Connect\x1b[0m');

    // Prepend the icon
    messageParts.unshift(` ${icon}`);
  }

  const message = messageParts.join(' ');

  if (category === LogCategory.Success || category === LogCategory.Error) {
    return `\n${message}\n`;
  } else {
    return message;
  }
}

/**
 * Makes a decorated log line. Uses different the `console` API methods
 * depending on the category.
 */
export function log(category: LogCategory, ...args: any) {
  const methodsByLogCategory: {
    [key in LogCategory]: string & keyof typeof console
  } = {
    [LogCategory.Error]: 'error',
    [LogCategory.Warn]: 'warn',
    [LogCategory.Success]: 'info',
    [LogCategory.Progress]: 'info',
    [LogCategory.Debug]: 'debug',
  };
  const methodName = methodsByLogCategory[category];
  console[methodName](decorateLogMessage(category, ...args));
}
