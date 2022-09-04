import { Component, OnInit, OnDestroy } from '@angular/core'
import { FormGroup, FormControl, Validators } from '@angular/forms'
import { Subscription } from 'rxjs'

import { DialogConfig } from '../dialog-config'
import { DialogRef } from '../dialog-ref'
import { InfoBoxService } from 'src/app/service/infobox.service'


@Component({
  selector: 'dialogChannel',
  templateUrl: './channel.dialog.html',
  styleUrls: ['./channel.dialog.scss']
})
export class DialogChannel implements OnInit, OnDestroy {
  private mSubscriptions: Array<Subscription> = new Array()

  public mFormGroup = new FormGroup({
    componentName: new FormControl('NONE', [Validators.required]),
    channelName: new FormControl(null)
  })

  public mType: 'input' | 'output'
  public mComponents: Map<string, Map<string, Array<string>>>
  public mSelected: string[]
  public mVariables: Map<string, Map<string, Array<string>>>
  public mVariableTranslations: {
    "SGCFOOON": "Test1"
  }

  constructor(
    mDialogConfig: DialogConfig,
    private mInfoBoxService: InfoBoxService,
    public mDialogRef: DialogRef) {
      this.mType = mDialogConfig.data.type
      this.mComponents = mDialogConfig.data.components

      if (mDialogConfig.data.variables) {
        const lPolls = new Array<string>()

        for (const eVariable in mDialogConfig.data.variables)
          lPolls.push(eVariable)

        const lVariables = {}
        lVariables['polls'] = lPolls
        lVariables['channels'] = lPolls

        this.mComponents['variables'] = lVariables
      }

      if (mDialogConfig.data.selected) {
        this.mFormGroup.controls.componentName.setValue(mDialogConfig.data.selected[0])
        this.mFormGroup.controls.channelName.setValue(mDialogConfig.data.selected[1])
      }
    }


  ngOnInit() {
    this.mSubscriptions.push(this.mFormGroup.controls.componentName.valueChanges.subscribe(
      (componentName: string) => {
        if (componentName != 'NONE') {
          if (this.mType == 'input') {
            if (this.mComponents[componentName].broadcasts)
              this.mFormGroup.controls.channelName.setValue(this.mComponents[componentName].broadcasts[0])
            else if (this.mComponents[componentName].polls)
              this.mFormGroup.controls.channelName.setValue(this.mComponents[componentName].polls[0])
          } else
            this.mFormGroup.controls.channelName.setValue(this.mComponents[componentName].channels[0])

          this.mFormGroup.controls.channelName.setValidators([Validators.required])
        } else {
          this.mFormGroup.controls.channelName.setValue(null)
          this.mFormGroup.controls.channelName.clearValidators()
        }
    }))

    window.setTimeout(() => this.mInfoBoxService.mText = 'empty')
  }


  ngOnDestroy() {
    this.mSubscriptions.forEach(value => value.unsubscribe())
  }
  

  public save(): void {
    let result = null

    if (this.mFormGroup.controls.componentName.value != 'NONE') {
      if (this.mType == 'input') {
        if (this.mFormGroup.controls.channelName.value.startsWith('_'))
          result = ['component', this.mFormGroup.controls.componentName.value, this.mFormGroup.controls.channelName.value.substr(1), 'poll']
        else
          result = ['component', this.mFormGroup.controls.componentName.value, this.mFormGroup.controls.channelName.value, 'stream']
      } else {
        result = ['component', this.mFormGroup.controls.componentName.value, this.mFormGroup.controls.channelName.value, 'stream']
      }
    }

    this.mDialogRef.close(true, result)
  }
}
