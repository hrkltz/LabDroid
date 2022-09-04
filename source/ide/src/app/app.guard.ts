import { Injectable } from '@angular/core'
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router'

import { WebSocketService } from './service/web-socket.service'
import { Observable } from 'rxjs'
import { DialogService } from './dialog/dialog.service'
import { first } from 'rxjs/operators'
import { DialogBoolean } from './dialog/generic/boolean/boolean.component'
import { DialogUnsaved } from './dialog/unsaved/unsaved.component'


export interface ComponentCanDeactivate {
  canDeactivate: () => boolean | Observable<boolean>;
}


@Injectable({
  providedIn: 'root'
})
export class AppGuard implements CanActivate {
  constructor(
    private mRouter: Router,
    private mDialogService: DialogService,
    private mWebSocketService: WebSocketService) {}


  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (this.mWebSocketService.mSystem && this.mWebSocketService.mSystem.projectName) {
      if (state.url != `/${this.mWebSocketService.mSystem.projectName}/log`) {
        this.mRouter.navigate(['/', this.mWebSocketService.mSystem.projectName, 'log'], {skipLocationChange: true})
        return false
      }
    }

    return true
  }


  canDeactivate(component: ComponentCanDeactivate): boolean | Observable<boolean> {
    // if there are no pending changes, just allow deactivation; else confirm first
    if (component.canDeactivate())
      return true

    return this.mDialogService.open(DialogUnsaved, {}).afterClosedSimple
      // NOTE: this warning message will only be shown when navigating elsewhere within your angular app;
      // when navigating away from your angular app, the browser will show a generic warning message
      // see http://stackoverflow.com/a/42207299/7307355
      //confirm('WARNING: You have unsaved changes. Press Cancel to go back and save these changes, or OK to lose these changes.');
  }
}