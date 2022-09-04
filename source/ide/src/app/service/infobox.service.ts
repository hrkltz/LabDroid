import { Injectable } from '@angular/core'



@Injectable({
  providedIn: 'root'
})
export class InfoBoxService {
  public mHasFocus: boolean = document.hasFocus()
  public mText: string = 'empty'
}
