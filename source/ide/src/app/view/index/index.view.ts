import { Component, HostListener } from '@angular/core'
import { Router } from '@angular/router'
import { first } from 'rxjs/operators'

import { WebSocketService } from '../../service/web-socket.service'
import { DialogService } from '../../dialog/dialog.service'
import { DialogNewProject } from 'src/app/dialog/new_project/new_project.dialog'
import { Title } from '@angular/platform-browser'
import { InfoBoxService } from 'src/app/service/infobox.service'
import { DialogBoolean } from 'src/app/dialog/generic/boolean/boolean.component'

@Component({
  selector: 'app-index',
  templateUrl: './index.view.html',
  styleUrls: ['./index.view.scss']
})
export class IndexComponent {
  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    const target = Array.from(document.querySelectorAll( ":hover" )).pop()
    
    switch(target.tagName.toLowerCase()) {
      case 'article':
        const projectName = target.id

        if (event.ctrlKey) {
          switch (event.key) {
            case 'd':
              this.mDialogService.open(DialogBoolean, {
                data: {
                  header: 'Delete Project',
                  main: 'Are you sure?'
                }
              }).afterClosedSimple.pipe(first()).subscribe((ppResult) => {
                if (ppResult)
                  this.mWebSocket.Project2.delete(projectName)
              })

              return( false )
          }
        }
      break
    }
  }


  constructor(
    title: Title,
    private mDialogService: DialogService,
    private mRouter: Router,
    public mInfoBoxService: InfoBoxService,
    public mWebSocket: WebSocketService) {
      title.setTitle(`LabDroid`)
    }


  public navigate(pUrl: string) {
    this.mRouter.navigate([pUrl], { skipLocationChange: true })
  }


  public create(): void {
    const ref = this.mDialogService.open(DialogNewProject, {
      data: {
        examples: this.mWebSocket.mSystem.examples,
        projects: this.mWebSocket.mSystem.projects
      }
    })

    ref.afterClosed.subscribe((pResult) => {
      if (pResult.success) {
        if (pResult.value.example != 'Empty') {
          this.mWebSocket.System2.save$.pipe(first()).subscribe((ppResult) => {
            if (ppResult)
              this.navigate(pResult.value.name)
          })

          this.mWebSocket.System2.save({
            'copyExample': [ pResult.value.name, pResult.value.example ]
          })
        } else {
          this.mWebSocket.Project2.create$.pipe(first()).subscribe((ppResult) => {
            if (ppResult)
              this.navigate(pResult.value.name)
          })

          this.mWebSocket.Project2.create(pResult.value.name)
        }
      }
    })
  }
}