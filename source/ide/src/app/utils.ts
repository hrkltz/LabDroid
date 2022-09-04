export class Utils {
    private static mCharacters: string = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'


    public static randomString(length: number): string {
        var result = ''

        for (; length > 0; length--)
           result += this.mCharacters.charAt(Math.floor(Math.random() * this.mCharacters.length))

        return result
     }
}