import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:travel_guide/features/bloc/list_of_countries_bloc.dart';

import '../repository/list_of_countries_repository_test.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late CountriesListDataBloc bloc;
  setUpAll(() {
    bloc = CountriesListDataBloc();
  });

  test('get successful data from bloc', () async {
    await dotenv.load(fileName: ".env");
    await bloc.getCountriesListData(service: MockCountriesListService());
    expect(bloc.getCountriesListDataStream$.valueOrNull?.first.name,
        'Afghanistan');
    expect(bloc.getCountriesListDataStream$.valueOrNull?.first.url,
        'https://travelbriefing.org/Afghanistan?format=json');
  });

  test('get bad data in bloc', () async {
    await dotenv.load(fileName: ".env");
    await bloc.getCountriesListData(service: MockCountriesListServiceBadData());
    expect(bloc.getCountriesListDataStream$.valueOrNull?.first.name, null);
    expect(bloc.getCountriesListDataStream$.valueOrNull?.first.url, null);
  });

  test('dispose of bloc closes stream', () async {
    bloc.dispose();
    expect(bloc.countriesListDataFetcher.isClosed, true);
  });
}
