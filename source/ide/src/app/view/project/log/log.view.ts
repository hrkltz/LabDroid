import { Component, OnInit, OnDestroy } from '@angular/core';


class LogElement {
  public timestamp: number = 0
  public message: string = ''
  public time: string = ''
  public type: number = 0
  public source: string = ''
}


@Component({
  selector: 'chmlnProjectLogView',
  templateUrl: './log.view.html',
  styleUrls: ['./log.view.scss']
})
export class LogView implements OnInit, OnDestroy {
  private mLogWebSocket: WebSocket | null = null;

  public mData: Array<LogElement> = new Array<LogElement>()


  constructor() {}

  ngOnInit() {
    this.connect()
  }


  ngOnDestroy() {
    if (this.mLogWebSocket && this.mLogWebSocket.readyState == WebSocket.OPEN)
      this.mLogWebSocket.close()
  }


  public connect(): void {
    this.mLogWebSocket = new WebSocket(`ws://${window.location.hostname}:8081/log`)
    this.mLogWebSocket.onmessage = this.onMessage.bind(this)
  }


  private onMessage(this: LogView, ev: MessageEvent): any {
    const lObject = JSON.parse(ev.data) as LogElement
    this.mData.push(lObject)
    /*lLogElement.timestamp = eMessage.timestamp
    lLogElement.time = eMessage.time
    lLogElement.msg = eMessage.msg
    lLogElement.type = eMessage.type
    lLogElement.source = eKey

    let lData: Array<LogElement> = new Array<LogElement>()

    Object.keys(lObject).forEach((eKey, e) => {
        lObject[eKey].forEach(eMessage => {
          // TODO Do we need to make this timeconsuming copy? Could be clone issue

          lData.push(lLogElement)
        });
    });

    console.log(lObject)

    this.mData = lData.concat(this.mData)

    this.mData.sort((a, b) => {
        return b.timestamp - a.timestamp
    })*/
  }


  public trackByFunction(pIndex: number, pItem: LogElement): any {
    return pItem.timestamp;
  }
}