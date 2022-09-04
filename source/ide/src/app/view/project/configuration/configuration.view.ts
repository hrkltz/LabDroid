import { Component } from '@angular/core'
import { WebSocketService } from '../../../service/web-socket.service'
import { DialogService } from '../../../dialog/dialog.service'
import { ActivatedRoute, Router } from '@angular/router'
import { DialogSelect } from 'src/app/dialog/generic/select/select.component'
import { first } from 'rxjs/operators'



@Component({
  selector: 'chmlnProjectConfigurationView',
  templateUrl: './configuration.view.html',
  styleUrls: ['./configuration.view.scss']
})
export class ConfigurationView {
  public mProjectName: string
  public mConfigurationBlueprints: Map<string, Map<string, any>>
  public mConfigurations: Map<string, any> = new Map()


  constructor(
    pActivatedRoute: ActivatedRoute,
    private mRouter: Router,
    private mDialogService: DialogService,
    public mWebSocketService: WebSocketService) {
      this.mProjectName = pActivatedRoute.snapshot.paramMap.get('projectName')

      this.mWebSocketService.mShowSide = true
      
      if (this.mWebSocketService.mSystem == null)
        this.mRouter.navigateByUrl('');
  }


  public addSettings() {
    let components = new Array()

    Object.keys(this.mWebSocketService.mSystem.components).forEach((eValue, eIndex) => {
      if (Array.from(this.mWebSocketService.mProject.configurations.keys()).filter(value => value == eValue).length == 0)
        components.push(eValue)
    })


    const ref = this.mDialogService.open(DialogSelect, {
      data: {
        'header': 'Component',
        'values': components
      }
    })

    ref.afterClosed.subscribe((pResult) => {
      if (pResult.success) {
        this.mWebSocketService.Component.create$.pipe(first()).subscribe((ppResult) => {
          if (ppResult)
            this.mRouter.navigate(['/', this.mProjectName, 'config', pResult.value], { skipLocationChange: true })
        })

        this.mWebSocketService.Component.create(this.mProjectName, pResult.value)
      }
    })
  }
}