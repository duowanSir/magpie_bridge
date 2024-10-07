import { v4 as uuidv4 } from 'uuid'
import {
    CallbackCmd,
    CallbackResult,
    IMagpieBridgeAsyncHandler,
    IMagpieBridgeContainer,
    IMagpieBridgeDispatcher,
    IMagpieBridgePushHandler,
    IMagpieBridgeSyncHandler,
    MagpieContext
} from './magpie-bridge-api'

/**
 * 参考：https://www.totaltypescript.com/how-to-properly-type-window
 */

const TAG = "MagpieBridge"
const ERROR_RESULT_NOT_FOUND_COMMANDER = 'not_found_commander'


class MagpieBridge implements IMagpieBridgeContainer, IMagpieBridgeDispatcher {
    private pushHandlers: { [key: string]: IMagpieBridgePushHandler[] } = {}
    private requestSyncHandlers: { [key: string]: IMagpieBridgeSyncHandler } = {}
    private requestAsyncHandlers: { [key: string]: IMagpieBridgeAsyncHandler } = {}
    private innerCallbackHandlers: { [key: string]: (result: string) => void } = {}

    addPushHandler(cmd: string, handler: IMagpieBridgePushHandler) {
        let handlers = this.pushHandlers[cmd]
        if (!handlers) {
            handlers = []
        }
        handlers.push(handler)
        this.pushHandlers[cmd] = handlers
    }

    registerSyncRequestHandler(cmd: string, handler: IMagpieBridgeSyncHandler) {
        this.requestSyncHandlers[cmd] = handler
    }

    registerAsyncRequestHandler(cmd: string, handler: IMagpieBridgeAsyncHandler) {
        this.requestAsyncHandlers[cmd] = handler
    }

    pushRequest(cmd: string, args: string): void {
        let magpieContext = new MagpieContext(generateShortUUID(), getFormattedDateTime())
        window.magpieBridgeAndroid.onReceivePushFromJs(JSON.stringify(magpieContext), cmd, args)
    }

    syncRequest(cmd: string, args: string): CallbackResult {
        let magpieContext = new MagpieContext(generateShortUUID(), getFormattedDateTime())
        return window.magpieBridgeAndroid.onReceiveRequestFromJs(JSON.stringify(magpieContext), cmd, args, '')
    }

    asyncRequest(cmd: string, args: string, callback: (result: string) => void): void {
        let callbackCmd = uuidv4()
        this.innerCallbackHandlers[callbackCmd] = callback
        let magpieContext = new MagpieContext(generateShortUUID(), getFormattedDateTime())
        window.magpieBridgeAndroid.onReceiveRequestFromJs(JSON.stringify(magpieContext), cmd, args, callbackCmd)
    }

    handlePush(context: MagpieContext, cmd: string, args: string) {
        let handlers = this.pushHandlers[cmd]
        if (!handlers || handlers.length == 0) {
            console.error(`${TAG}.handlePush failed because hasn't push handler cmd=${cmd}`)
            return
        }
        handlers.forEach((value, index) => {
            value.handle(context, cmd, args)
        })
    }

    handleAsyncRequest(context: MagpieContext, cmd: string, args: string, callbackCmd: string) {
        let handler = this.requestAsyncHandlers[cmd]
        if (!handler) {
            console.error(`${TAG}.handleAsyncRequest failed because hasn't request handler cmd=${cmd}`)
            this.pushRequest(callbackCmd, ERROR_RESULT_NOT_FOUND_COMMANDER)
            return
        }
        handler.handle(context, cmd, args, (result) => {
            this.pushRequest(callbackCmd, result)
        })
    }

    handleSyncRequest(context: MagpieContext, cmd: string, args: string): string {
        let handler = this.requestSyncHandlers[cmd]
        if (!handler) {
            console.error(`${TAG}.handleSyncRequest failed because hasn't request handler cmd=${cmd}`)
            return ERROR_RESULT_NOT_FOUND_COMMANDER
        }
        return handler.handle(context, cmd, args)
    }

}

interface IMagpieBridgeAndroid {
    onReceivePushFromJs(context: string, cmd: string, args: string): void
    onReceiveRequestFromJs(context: string, cmd: String, args: String, callbackCmd: CallbackCmd): CallbackResult
}

export type MagpieBridgeAndroid = IMagpieBridgeAndroid

declare global {
    interface Window {
        magpieBridgeAndroid: MagpieBridgeAndroid
        magpieBridge: IMagpieBridgeContainer
        magpieBridgeDispatcher: IMagpieBridgeDispatcher
        onReceivePushFromNative: (context: string, cmd: string, args: string) => void
        onReceiveRequestFromNative: (context: string, cmd: string, args: string, callbackCmd: CallbackCmd) => CallbackResult
    }
}

// 接收来自native的消息
function onReceivePushFromNative(context: string, cmd: string, args: string): void {
    console.log(`${TAG}.onReceivePushFromNative context=${context} cmd=${cmd} args=${args}`)
    const magpieContext: MagpieContext = JSON.parse(context) as MagpieContext
    window.magpieBridgeDispatcher.handlePush(magpieContext, cmd, args)
}


// 接收来自native的消息
function onReceiveRequestFromNative(context: string, cmd: string, args: string, callbackCmd: CallbackCmd): CallbackResult {
    console.log(`${TAG}.onReceiveRequestFromNative context=${context} cmd=${cmd} args=${args} callbackCmd=${callbackCmd}`)
    const magpieContext: MagpieContext = JSON.parse(context) as MagpieContext
    if (callbackCmd.length === 0) {
        return window.magpieBridgeDispatcher.handleSyncRequest(magpieContext, cmd, args)
    } else {
        window.magpieBridgeDispatcher.handleAsyncRequest(magpieContext, cmd, args, callbackCmd)
        return ""
    }
}

function generateShortUUID(): string {
    const uuid = uuidv4()
    return uuid.substring(0, 8) // 取前8位字符
}

export function getFormattedDateTime(): string {
    const date = new Date()

    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0') // 月份从0开始，需要+1
    const day = String(date.getDate()).padStart(2, '0')

    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')

    const milliseconds = String(date.getMilliseconds()).padStart(3, '0') // 确保有三位数

    return `${year}-${month}-${day}:${hours}-${minutes}-${seconds}:${milliseconds}`
}

export function useMagpieBridge() {
    console.log('magpie bridge init')
    if (!window.magpieBridgeAndroid) {
        console.error("magpie bridge android loose")
        return
    }
    if (window.magpieBridge
        || window.magpieBridgeDispatcher
    ) {
        console.error("magpie bridge occupied")
        return
    }
    // 全局注册 MagpieBridge
    const magpieBridge = new MagpieBridge()
    window.magpieBridge = magpieBridge
    window.magpieBridgeDispatcher = magpieBridge
    window.onReceivePushFromNative = onReceivePushFromNative
    window.onReceiveRequestFromNative = onReceiveRequestFromNative

    magpieBridge.pushRequest('EventJsReady', '')
    console.log('magpie bridge is ready')
    // 你可以添加更多交互逻辑
}

// 导出IMagpieBridgeJ2N接口，以便在主模块中使用
export {
    IMagpieBridgeContainer,
    MagpieContext,
    IMagpieBridgePushHandler,
    IMagpieBridgeSyncHandler,
    IMagpieBridgeAsyncHandler
} from './magpie-bridge-api'
