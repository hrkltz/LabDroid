export function deepCopy(pObject: any): any {
  return JSON.parse(JSON.stringify(pObject));
}


export function objectToMap(pObject: Object): Map<string, any> {
  return new Map(Object.entries(pObject));
}