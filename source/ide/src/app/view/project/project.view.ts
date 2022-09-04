import { Component } from '@angular/core'
import { map } from 'rxjs/operators'
import { Router, ActivatedRoute } from '@angular/router'
import { Title } from '@angular/platform-browser'

import { SystemObject, WebSocketService } from 'src/app/service/web-socket.service'


@Component({
  selector: 'chmlnProjectView',
  templateUrl: './project.view.html',
  styleUrls: ['./project.view.scss']
})
export class ProjectComponent {
  public mProjectName: string = ''
  public mSystem: SystemObject = new SystemObject()


  constructor(
    title: Title,
    activatedRoute: ActivatedRoute,
    public mWebSocket: WebSocketService,
    public mRouter: Router) {
      activatedRoute.paramMap
        .pipe(map(params => params.get('projectName') ))
        .subscribe(projectName => {
          if (projectName == null)
            return
  
          this.mProjectName = projectName
          title.setTitle(`LabDroid: ${this.mProjectName}`)
        })

      this.mWebSocket.Project2.load(this.mProjectName)

      
      // this.mWebSocket.File.load$.subscribe((eFile) => {
      //   console.log(eFile)
      // })
  
      this.mWebSocket.File.load(this.mProjectName, '')
  }
  

  public toggleExecution(): void {
    if (this.mWebSocket.mSystem.projectName == null) {
      this.mWebSocket.System2.save({'projectName': this.mProjectName})
      this.mRouter.navigate(['/', this.mProjectName, 'log'], {skipLocationChange: true});
    }
    else
      this.mWebSocket.System2.save({'projectName': null})
  }


  public navigateOrCloseSide(navigationTarget: Array<string>) {
    if (this.mRouter.url.includes(navigationTarget[1]))
      this.mWebSocket.mShowSide = !this.mWebSocket.mShowSide
    else
      this.mRouter.navigate(navigationTarget, { skipLocationChange: true })
  }
}