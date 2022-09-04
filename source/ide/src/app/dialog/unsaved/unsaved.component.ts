import { Component, OnInit } from '@angular/core'

import { DialogConfig } from '../dialog-config'
import { DialogRef } from '../dialog-ref'
import { InfoBoxService } from 'src/app/service/infobox.service'


@Component({
    selector: 'dialogUnsaved',
    templateUrl: './unsaved.component.html',
    styleUrls: ['./unsaved.component.scss']
})
export class DialogUnsaved implements OnInit {
  constructor(
    private mInfoBoxService: InfoBoxService,
    public mDialogConfig: DialogConfig,
    public mDialogRef: DialogRef) {}


  ngOnInit() {
    window.setTimeout(() => this.mInfoBoxService.mText = 'empty')
  }
}