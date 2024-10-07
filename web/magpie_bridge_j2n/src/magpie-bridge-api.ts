export type CallbackCmd = string
export type CallbackResult = string
export type Callback = (args: string) => void

export interface IMagpieBridgePushHandler {
    handle(context: MagpieContext, cmd: string, args: string): void
}

// 同步接口
export interface IMagpieBridgeSyncHandler {
    handle(context: MagpieContext, cmd: string, args: string): string
}

// 异步接口
export interface IMagpieBridgeAsyncHandler {
    handle(context: MagpieContext, cmd: string, args: string, callback: (result: string) => void): void
}

export interface IMagpieBridgeJ2N {
    pushRequest(cmd: string, args: string): void
    syncRequest(cmd: string, args: string): CallbackResult
    asyncRequest(cmd: string, args: string, callback: (result: string) => void): void
}

export interface IMagpieBridgeDispatcher {
    handlePush(context: MagpieContext, cmd: string, args: string): void
    handleSyncRequest(context: MagpieContext, cmd: string, args: string): string
    handleAsyncRequest(context: MagpieContext, cmd: string, args: string, callbackCmd: string): void
}

export interface IMagpieBridgeContainer extends IMagpieBridgeJ2N {
    addPushHandler(cmd: string, handler: IMagpieBridgePushHandler): void
    registerSyncRequestHandler(cmd: string, handler: IMagpieBridgeSyncHandler): void
    registerAsyncRequestHandler(cmd: string, handler: IMagpieBridgeAsyncHandler): void
}

export class MagpieContext {
    traceId: string;
    beginTime: string;

    constructor(traceId: string, beginTime: string) {
        this.traceId = traceId;
        this.beginTime = beginTime;
    }
}
