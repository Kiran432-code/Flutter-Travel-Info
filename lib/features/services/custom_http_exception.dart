import 'dart:io';

class InternalServerException implements HttpException {
  const InternalServerException(this.message, {required this.uri});
  @override
  final String message;
  @override
  final Uri uri;

  @override
  String toString() {
    final StringBuffer b = StringBuffer()
      ..write('InternalServerException: ')
      ..write(message);
    final Uri uri = this.uri;
    b.write(', uri = $uri');
    return b.toString();
  }
}
