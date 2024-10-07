// src/MagpieBridgeLogger.tsx
import React, { useEffect, useState } from 'react'
import {
    IMagpieBridgeContainer,
    MagpieContext,
    IMagpieBridgePushHandler,
    IMagpieBridgeSyncHandler,
    IMagpieBridgeAsyncHandler,
    useMagpieBridge,
    getFormattedDateTime
    //   } from 'magpie_bridge_j2n'
} from '../magpie_bridge_j2n/src/magpie-bridge'

declare global {
    interface Window {
        magpieBridge: IMagpieBridgeContainer
    }
}

enum TestCommandN2J {
    Loop
}

enum TestCommandJ2N {
    Print
}

const MagpieBridgeLogger: React.FC = () => {
    const [logs, setLogs] = useState<string[]>([])

    const addLog = (newLog: string) => {
        setLogs((prevLogs) => [...prevLogs, newLog]);
    };

    useEffect(() => {
        useMagpieBridge()

        let loopPushHandler: IMagpieBridgePushHandler = {
            handle: (context: MagpieContext, cmd: string, args: string) => {
                console.log(`Handling push command: ${cmd}, with args: ${args}`)
                addLog(`[${context.beginTime}-${getFormattedDateTime()}] ${context.traceId} ${cmd} ${args}`)
                window.magpieBridge.pushRequest(TestCommandJ2N[TestCommandJ2N.Print], `${args}-${context.traceId}`)
            }
        }
        window.magpieBridge.addPushHandler(TestCommandN2J[TestCommandN2J.Loop], loopPushHandler)

        let loopSyncHandler: IMagpieBridgeSyncHandler = {
            handle: function (context: MagpieContext, cmd: string, args: string): string {
                console.log(`Handling sync command: ${cmd}, with args: ${args}`)
                addLog(`[${context.beginTime}-${getFormattedDateTime()}] ${context.traceId} ${cmd} ${args}`)
                let result = window.magpieBridge.syncRequest(TestCommandJ2N[TestCommandJ2N.Print], `${args}-${context.traceId}`)
                addLog(result)
                return `[${getFormattedDateTime()}] ${context.traceId} JS: sync ok`
            }
        }
        window.magpieBridge.registerSyncRequestHandler(TestCommandN2J[TestCommandN2J.Loop], loopSyncHandler)

        let loopAsyncHandler: IMagpieBridgeAsyncHandler = {
            handle: function (context: MagpieContext, cmd: string, args: string, callback: (result: string) => void): void {
                console.log(`Handling async command: ${cmd}, with args: ${args}`)
                window.magpieBridge.asyncRequest(TestCommandJ2N[TestCommandJ2N.Print], args, (result) => {
                    console.log(`Handling async callback result:${result}`)
                })
                callback('JS: async ok')
            }
        }
        window.magpieBridge.registerAsyncRequestHandler(TestCommandN2J[TestCommandN2J.Loop], loopAsyncHandler)
    }, [])

    return (
        <div>
            <h2>MagpieBridge Log</h2>
            <div style={{ border: '1px solid #ccc', padding: '10px', maxHeight: '200px', overflowY: 'auto' }}>
                {logs.map((log, index) => (
                    <div key={index}>{log}</div>
                ))}
            </div>
        </div>
    )
}

export default MagpieBridgeLogger