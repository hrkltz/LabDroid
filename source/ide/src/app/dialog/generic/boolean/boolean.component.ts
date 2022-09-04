import { Component, OnInit } from '@angular/core'

import { DialogConfig } from '../../dialog-config'
import { DialogRef } from '../../dialog-ref'
import { InfoBoxService } from 'src/app/service/infobox.service'


@Component({
    selector: 'dialogBoolean',
    templateUrl: './boolean.component.html',
    styleUrls: ['./boolean.component.scss']
})
export class DialogBoolean implements OnInit {
  constructor(
    private mInfoBoxService: InfoBoxService,
    public mDialogConfig: DialogConfig,
    public mDialogRef: DialogRef) {}


  ngOnInit() {
    window.setTimeout(() => this.mInfoBoxService.mText = 'empty')
  }
}