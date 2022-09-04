import { Component, HostListener } from '@angular/core';
import { WebSocketService } from './service/web-socket.service';
import { TranslateService } from '@ngx-translate/core';
import { InfoBoxService } from './service/infobox.service';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  public mStatus = {
    CONNECTING: WebSocket.CONNECTING,
    OPEN: WebSocket.OPEN,
    CLOSING: WebSocket.CLOSING,
    CLOSED: WebSocket.CLOSED
  }
  public mShowInfoBox: boolean = true

  @HostListener('window:focus', ['$event'])
  handleFocusEvent(event: Event) {
    this.mInfoBoxService.mHasFocus = true
  }


  @HostListener('window:blur', ['$event'])
  handleBlurEvent(event: Event) {
    this.mInfoBoxService.mHasFocus = false
  }


  // We include this to start the WebSocket a little bit earlier.
  constructor(
    pTranslateService: TranslateService,
    public mInfoBoxService: InfoBoxService,
    public mWebSocketService: WebSocketService) {
      // this language will be used as a fallback when a translation isn't found in the current language
      pTranslateService.setDefaultLang('en')
      // the lang to use, if the lang isn't available, it will use the current loader to get them
      pTranslateService.use('en')
    }
}
