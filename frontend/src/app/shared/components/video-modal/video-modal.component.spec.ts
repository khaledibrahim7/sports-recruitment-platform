import { parseSupportedVideoUrl } from './video-modal.component';

describe('parseSupportedVideoUrl', () => {
  it('accepts supported YouTube URL forms and extracts the video id', () => {
    expect(parseSupportedVideoUrl('https://youtu.be/abcdefghijk?si=xyz')?.youtubeId).toBe('abcdefghijk');
    expect(parseSupportedVideoUrl('https://www.youtube.com/watch?v=abcdefghijk')?.type).toBe('youtube');
  });

  it('rejects lookalike hosts and unsupported protocols', () => {
    expect(parseSupportedVideoUrl('https://youtube.com.attacker.example/watch?v=abcdefghijk')).toBeNull();
    expect(parseSupportedVideoUrl('javascript:alert(1)')).toBeNull();
  });

  it('accepts known provider paths and direct video files only', () => {
    expect(parseSupportedVideoUrl('https://drive.google.com/file/d/abc123/view')?.type).toBe('drive');
    expect(parseSupportedVideoUrl('https://player.vimeo.com/video/123456')?.type).toBe('vimeo');
    expect(parseSupportedVideoUrl('https://cdn.example.com/clip.mp4?token=ok')?.type).toBe('direct');
    expect(parseSupportedVideoUrl('https://unknown.example.com/watch/1')).toBeNull();
  });
});
